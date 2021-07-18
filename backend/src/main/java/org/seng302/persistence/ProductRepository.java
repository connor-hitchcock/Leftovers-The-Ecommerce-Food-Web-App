package org.seng302.persistence;

import org.seng302.entities.Business;
import org.seng302.entities.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long>{

        /**
         *
         * @param business the business
         * @param productCode the code of the product
         * @return a single product within the business's catalogue that matches the id of said business and the code
         * of the product
         */
        Optional<Product> findByBusinessAndProductCode(@Param("business") Business business,
                                                       @Param("productCode") String productCode);

        List<Product> getAllByBusiness(@Param("Business") Business business);

        /**
         *
         * @param productCode
         * @return a single product within the business's catalogue that matches the the code
         *          * of the product
         */
        Optional<Product> findByProductCode(@Param("productCode") String productCode);

        /**
        * Find all then products in the repository which belong to the given business.
        * @param business The business which owns the products.
        * @return A list of products belonging to the business.
        */
        public List<Product> findAllByBusiness(@Param("business") Business business);

        /**
         * Gets a product from the repository.
         * If the product does not exist then a 406 Not Acceptable is thrown
         * If the product belongs to another business, a 403 Forbidden is thrown
         * @param business The business that has the product
         * @param productCode The productCode of the product
         * @return A product or ResponseStatusException
         */
        default Product getProductByBusinessAndProductCode(Business business, String productCode) {
                Optional<Product> product = this.findByProductCode(productCode);
                if (product.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                                "The given product does not exist");
                }
                if (!product.get().getBusiness().getId().equals(business.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You cannot modify this product");
                }
                return product.get();
        }
                /**
         * Gets a product from the database that matches a given image Id. This method preforms a sanity check to ensure the
         * image does exist and if not throws a not accepted response status exception.
         * @param business the business object
         * @param productCode the product code of the product
         * @return the product object that matches the business and product code
         */
        default Product getProduct(Business business, String productCode) {
                Optional<Product> product = findByBusinessAndProductCode(business, productCode);
                if (product.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                                "the product does not exist");
                }
                return product.get();
                // The product repo is not working as expected, the product can still be retrieved even when it does not exist
                // within the business's catalogue
        }

}