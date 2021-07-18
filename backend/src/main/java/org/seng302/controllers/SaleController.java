package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.SaleItem;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.SaleItemRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.SearchHelper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class SaleController {
    private static final Logger logger = LogManager.getLogger(SaleController.class);

    private final BusinessRepository businessRepository;
    private final SaleItemRepository saleItemRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public SaleController(BusinessRepository businessRepository, SaleItemRepository saleItemRepository, InventoryItemRepository inventoryItemRepository) {
        this.businessRepository = businessRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public Comparator<SaleItem> getSaleItemComparator(String orderBy) {
        if (orderBy == null) orderBy = "created";
        switch (orderBy) {
            case "created":
                return Comparator.comparing(SaleItem::getCreated);
            case "closing":
                return Comparator.comparing(SaleItem::getCloses);
            case "productCode":
                return Comparator.comparing(saleItem -> saleItem.getProduct().getProductCode());
            case "productName":
                return Comparator.comparing(saleItem -> saleItem.getProduct().getName(), String.CASE_INSENSITIVE_ORDER);
            case "quantity":
                return Comparator.comparing(SaleItem::getQuantity);
            case "price":
                return Comparator.comparing(SaleItem::getPrice);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort order");
        }
    }

    @PostMapping("/businesses/{id}/listings")
    public JSONObject addSaleItemToBusiness(@PathVariable Long id, @RequestBody JSONObject saleItemInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding sales item to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);
            business.checkSessionPermissions(request);

            if (saleItemInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale item creation info not provided");
            }
            Object inventoryItemIdObj = saleItemInfo.get("inventoryItemId");
            if (!(inventoryItemIdObj instanceof Number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventoryItemId not a number");
            }
            InventoryItem inventoryItem;
            try {
                inventoryItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                        business,
                        ((Number)inventoryItemIdObj).longValue()
                );
            } catch (ResponseStatusException exception) {
                // Make sure to return a 400 instead of a 406
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getReason());
            }

            if (!(saleItemInfo.get("quantity") instanceof Integer)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity not a integer");
            }

            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withQuantity((Integer)saleItemInfo.get("quantity"))
                    .withPrice(saleItemInfo.getAsString("price"))
                    .withMoreInfo(saleItemInfo.getAsString("moreInfo"))
                    .withCloses(saleItemInfo.getAsString("closes"))
                    .build();
            saleItem = saleItemRepository.save(saleItem);

            response.setStatus(201);
            var object = new JSONObject();
            object.put("listingId", saleItem.getSaleId());
            return object;
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * REST GET method to retrieve all the sale items for a given business
     * @param id the id of the business
     * @param request the HTTP request
     * @return List of sale items the business is listing
     */
    @GetMapping("/businesses/{id}/listings")
    public JSONArray getSaleItemsForBusiness(@PathVariable Long id,
                                             HttpServletRequest request,
                                             @RequestParam(required = false) String orderBy,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer resultsPerPage,
                                             @RequestParam(required = false) Boolean reverse) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Getting sales item for business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);

            List<SaleItem> listings = new ArrayList<>(saleItemRepository.findAllForBusiness(business));

            Comparator<SaleItem> comparator = getSaleItemComparator(orderBy);
            if (Boolean.TRUE.equals(reverse)) {
                comparator = comparator.reversed();
            }
            listings.sort(comparator);

            listings = SearchHelper.getPageInResults(listings, page, resultsPerPage);

            var response = new JSONArray();
            for (SaleItem saleItem : listings) {
                response.add(saleItem.constructJSONObject());
            }
            return response;
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * REST GET method to retrieve the sale item count for the business
     * @param id the id of the business
     * @param request the HTTP request
     * @return JSON object containing a single "count" field with the sales item count
     */
    @GetMapping("/businesses/{id}/listings/count")
    public JSONObject getSalesItemForBusinessCount(@PathVariable Long id, HttpServletRequest request) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Getting sales item count for business (businessId=%d", id));
            Business business = businessRepository.getBusinessById(id);

            List<SaleItem> listings = saleItemRepository.findAllForBusiness(business);

            var response = new JSONObject();
            response.put("count", listings.size());

            return response;
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }
}
