package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.Image;
import org.seng302.entities.Product;
import org.seng302.exceptions.BusinessNotFoundException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ImageRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.service.StorageService;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This class handles requests for retrieving and saving products
 */
@RestController
public class ProductController {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private static final Logger logger = LogManager.getLogger(ProductController.class.getName());

    @Autowired
    public ProductController(ProductRepository productRepository, BusinessRepository businessRepository, StorageService storageService, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.storageService = storageService;
        this.imageRepository = imageRepository;
    }

    /**
     * Sort products by a key. Can reverse results.
     * @param key Key to order products by.
     * @param reverse Reverse results.
     * @return Product Comparator
     */
    Comparator<Product> sortProducts(String key, Boolean reverse) {
        key = key == null ? "productCode" : key;
        if (reverse == null) {
            reverse = false;
        }

        Comparator<Product> sort;
        switch (key) {
            case "name":
                sort = Comparator.comparing(Product::getName,
                        String.CASE_INSENSITIVE_ORDER);
                break;

            case "description":
                sort = Comparator.comparing(Product::getDescription,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;

            case "manufacturer":
                sort = Comparator.comparing(Product::getManufacturer,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;

            case "recommendedRetailPrice":
                sort = Comparator.comparing(Product::getRecommendedRetailPrice,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "created":
                sort = Comparator.comparing(Product::getCreated);
                break;

            default:
                sort = Comparator.comparing(Product::getProductCode);
                break;
        }

        if (reverse) {
            sort = sort.reversed();
        }

        return sort;
    }

    /**
     * REST GET method to retrieve all the products with a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products")
    public JSONArray retrieveCatalogue(@PathVariable Long id,
                                       HttpServletRequest request,
                                       @RequestParam(required = false) String orderBy,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer resultsPerPage,
                                       @RequestParam(required = false) Boolean reverse) {

        logger.info("Get catalogue by business id.");
        AuthenticationTokenManager.checkAuthenticationToken(request);

        logger.info(() -> String.format("Retrieving catalogue from business with id %d.", id));
        Optional<Business> business = businessRepository.findById(id);
        if (business.isEmpty()) {
            BusinessNotFoundException notFound = new BusinessNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            business.get().checkSessionPermissions(request);
            List<Product> catalogue = productRepository.getAllByBusiness(business.get());

            Comparator<Product> sort = sortProducts(orderBy, reverse);
            catalogue.sort(sort);

            catalogue = SearchHelper.getPageInResults(catalogue, page, resultsPerPage);

            JSONArray responseBody = new JSONArray();
            for (Product product: catalogue) {
                responseBody.appendElement(product.constructJSONObject());
            }
            return responseBody;
        }
    }

    /**
     * REST GET method to retrieve the number of products in a business's catalogue.
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of products in the business's catalogue
     */
    @GetMapping("/businesses/{id}/products/count")
    public JSONObject retrieveCatalogueCount(@PathVariable Long id,
                                             HttpServletRequest request) {

        AuthenticationTokenManager.checkAuthenticationToken(request);

        Optional<Business> business = businessRepository.findById(id);

        if(business.isEmpty()) {
            BusinessNotFoundException notFound = new BusinessNotFoundException();
            logger.error(notFound.getMessage());
            throw new BusinessNotFoundException();
        } else {
            business.get().checkSessionPermissions(request);

            List<Product> catalogue = productRepository.getAllByBusiness(business.get());

            JSONObject responseBody = new JSONObject();
            responseBody.put("count", catalogue.size());

            return responseBody;
        }
    }


    /**
     * POST endpoint for adding a product to a businesses catalogue.
     * This is only accessible to the DGAA, the business owner or a business admin.
     * @param id The business id to add a product to
     * @param productInfo The request body that should contain the product information
     * @param request Additional information about the request
     * @param response The response to this request
     */
    @PostMapping("/businesses/{id}/products")
    public void addProductToBusiness(@PathVariable Long id, @RequestBody JSONObject productInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding product to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);

            business.checkSessionPermissions(request);

            if (productInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product creation info not provided");
            }
            String productCode = productInfo.getAsString("id");

            if (productRepository.findByBusinessAndProductCode(business, productCode).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists with product code in this catalogue \"" + productCode + "\"");
            }

            Product product = new Product.Builder()
                    .withProductCode(productCode)
                    .withName(productInfo.getAsString("name"))
                    .withDescription(productInfo.getAsString("description"))
                    .withManufacturer(productInfo.getAsString("manufacturer"))
                    .withRecommendedRetailPrice(productInfo.getAsString("recommendedRetailPrice"))
                    .withBusiness(business)
                    .build();
            productRepository.save(product);

            response.setStatus(201);
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * Matches up the businessID, productID and imageID to find the image of a product to be deleted. Only business
     * owners can delete product images and they must be within their own product catalogue.
     * @param businessId the ID of the business
     * @param productId the ID of the product
     * @param imageId the ID of the image
     */
    @DeleteMapping("/businesses/{businessId}/products/{productId}/images/{imageId}")
    public void deleteProductImage(@PathVariable Long businessId, @PathVariable String productId,
                                   @PathVariable Long imageId,
                                   HttpServletRequest request) {
        logger.info(() -> String.format("Deleting image with id %d from the product %s within the business's catalogue %d",
                imageId, productId, businessId));

        Business business = businessRepository.getBusinessById(businessId); // get the business + sanity checks

        Product product = productRepository.getProductByBusinessAndProductCode(business, productId); // get the product + sanity checks

        Image image = imageRepository.getImageByProductAndId(product, imageId); // get the image + sanity checks

        business.checkSessionPermissions(request); // Can this user do this action

        product.removeProductImage(image);
        imageRepository.delete(image);
        storageService.deleteOne(image.getFilename());

        productRepository.save(product);
    }

    @PostMapping("/businesses/{businessId}/products/{productCode}/images")
    public ResponseEntity<Void> uploadImage(@PathVariable Long businessId, @PathVariable String productCode, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding product image to business (businessId=%d, productCode=%s).", businessId, productCode));
            Business business = businessRepository.getBusinessById(businessId);


            business.checkSessionPermissions(request);

            // Will throw 406 response status exception if product does not exist
            Product product = productRepository.getProduct(business, productCode);

            validateImage(file);

            String filename = UUID.randomUUID().toString();
            if ("image/jpeg".equals(file.getContentType())) {
                filename += ".jpg";
            } else if ("image/png".equals(file.getContentType())) {
                filename += ".png";
            } else {
                assert false; // We've already validated the image type so this should not be possible.
            }

            Image image = new Image(null, null);
            image.setFilename(filename);
            image = imageRepository.save(image);
            product.addProductImage(image);
            productRepository.save(product);
            storageService.store(file, filename);             //store the file using storageService

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
    /**
     * Sets the given image as the primary image for the given product
     * Only business administrators can perform this action.
     * @param businessId the ID of the business
     * @param productId the ID of the product
     * @param imageId the ID of the image
     */
    @PutMapping("/businesses/{businessId}/products/{productId}/images/{imageId}/makeprimary")
    public void makeImagePrimary(@PathVariable Long businessId,@PathVariable String productId,
                                 @PathVariable Long imageId,
                                 HttpServletRequest request ) {
        // get business + sanity
        Business business = businessRepository.getBusinessById(businessId);
        // check user priv
        business.checkSessionPermissions(request);

        // get product + sanity
        Product product = productRepository.getProductByBusinessAndProductCode(business, productId);
        // get image + sanity
        Image image = imageRepository.getImageByProductAndId(product, imageId);

        List<Image> images = product.getProductImages(); // get the images so we can manipulate them
        // If the given image is already the primary image, return
        if (images.get(0).getID().equals(image.getID())) {
            return;
        }

        images.remove(image); // pop the image from the list
        images.add(0, image); // append to the start of the list
        product.setProductImages(images); // apply the changes
        productRepository.save(product);
        logger.info(() -> String.format("Set Image %d of product \"%s\" as the primary image", image.getID(), product.getName()));
    }

    public void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if(contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image format. Must be jpeg or png");
        }
    }
}
