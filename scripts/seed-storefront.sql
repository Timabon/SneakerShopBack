-- Idempotent local storefront seed for the products shown on Home and Women.

INSERT INTO product (product_name, product_description, product_price)
SELECT
  'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT',
  'Traditioneller Court-Turnschuh im neuen Look mit strapazierfähiger Gummiaußensohle.',
  120.00
WHERE NOT EXISTS (
  SELECT 1 FROM product
  WHERE product_name = 'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT'
);

INSERT INTO product (product_name, product_description, product_price)
SELECT
  'SAMBA OG SCHUH',
  'Lederschuh mit einer längeren Zunge für einen auffälligen, vom Fußball inspirierten Look. Dieses Produkt ist von bestimmten Rabatten und Aktionen ausgeschlossen.',
  130.00
WHERE NOT EXISTS (
  SELECT 1 FROM product WHERE product_name = 'SAMBA OG SCHUH'
);

UPDATE product
SET
  product_description = 'Traditioneller Court-Turnschuh im neuen Look mit strapazierfähiger Gummiaußensohle.',
  product_price = 120.00
WHERE product_name = 'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT';

UPDATE product
SET
  product_description = 'Lederschuh mit einer längeren Zunge für einen auffälligen, vom Fußball inspirierten Look. Dieses Produkt ist von bestimmten Rabatten und Aktionen ausgeschlossen.',
  product_price = 130.00
WHERE product_name = 'SAMBA OG SCHUH';

DELETE FROM product_image_urls
WHERE product_product_id IN (
  SELECT product_id FROM product
  WHERE product_name IN (
    'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT',
    'SAMBA OG SCHUH'
  )
);

INSERT INTO product_image_urls (product_product_id, image_url)
SELECT product_id, image_url
FROM product
CROSS JOIN (
  VALUES
    ('/products/handball-spezial-leopard/01.avif'),
    ('/products/handball-spezial-leopard/02.avif'),
    ('/products/handball-spezial-leopard/03.avif'),
    ('/products/handball-spezial-leopard/04.avif')
) AS images(image_url)
WHERE product_name = 'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT';

INSERT INTO product_image_urls (product_product_id, image_url)
SELECT product_id, image_url
FROM product
CROSS JOIN (
  VALUES
    ('/products/samba-og-brown/01.avif'),
    ('/products/samba-og-brown/02.avif'),
    ('/products/samba-og-brown/03.avif'),
    ('/products/samba-og-brown/04.avif')
) AS images(image_url)
WHERE product_name = 'SAMBA OG SCHUH';

INSERT INTO catalog (catalog_name)
SELECT 'Women'
WHERE NOT EXISTS (
  SELECT 1 FROM catalog WHERE lower(catalog_name) = 'women'
);

INSERT INTO catalog (catalog_name)
SELECT 'Men'
WHERE NOT EXISTS (
  SELECT 1 FROM catalog WHERE lower(catalog_name) = 'men'
);

INSERT INTO catalog (catalog_name)
SELECT 'Unisex'
WHERE NOT EXISTS (
  SELECT 1 FROM catalog WHERE lower(catalog_name) = 'unisex'
);

INSERT INTO catalog_products (catalog_catalog_id, products_product_id)
SELECT catalog.catalog_id, product.product_id
FROM catalog
CROSS JOIN product
WHERE lower(catalog.catalog_name) = 'women'
  AND product.product_name IN (
    'HANDBALL SPEZIAL SCHUH, LEOPARDENPRINT',
    'SAMBA OG SCHUH'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM catalog_products
    WHERE catalog_catalog_id = catalog.catalog_id
      AND products_product_id = product.product_id
  );

INSERT INTO catalog_products (catalog_catalog_id, products_product_id)
SELECT catalog.catalog_id, product.product_id
FROM catalog
CROSS JOIN product
WHERE lower(catalog.catalog_name) = 'women'
  AND EXISTS (
    SELECT 1 FROM product_image_urls
    WHERE product_product_id = product.product_id
      AND image_url LIKE '%AIR%2BMAX%2BMOTO%'
  )
  AND NOT EXISTS (
    SELECT 1 FROM catalog_products
    WHERE catalog_catalog_id = catalog.catalog_id
      AND products_product_id = product.product_id
  );

INSERT INTO catalog_products (catalog_catalog_id, products_product_id)
SELECT catalog.catalog_id, product.product_id
FROM catalog
CROSS JOIN product
WHERE lower(catalog.catalog_name) = 'men'
  AND EXISTS (
    SELECT 1 FROM product_image_urls
    WHERE product_product_id = product.product_id
      AND image_url LIKE '%NIKE%2BSHOX%2BR4%'
  )
  AND NOT EXISTS (
    SELECT 1 FROM catalog_products
    WHERE catalog_catalog_id = catalog.catalog_id
      AND products_product_id = product.product_id
  );

INSERT INTO catalog_products (catalog_catalog_id, products_product_id)
SELECT catalog.catalog_id, product.product_id
FROM catalog
CROSS JOIN product
WHERE lower(catalog.catalog_name) = 'unisex'
  AND EXISTS (
    SELECT 1 FROM product_image_urls
    WHERE product_product_id = product.product_id
      AND image_url LIKE '%NIKE%2BDUNK%2BLOW%'
  )
  AND NOT EXISTS (
    SELECT 1 FROM catalog_products
    WHERE catalog_catalog_id = catalog.catalog_id
      AND products_product_id = product.product_id
  );
