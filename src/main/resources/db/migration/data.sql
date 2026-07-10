-- Activation de l'extension pgcrypto pour la génération des UUID (assure la compatibilité)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- TABLE : users
-- =====================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

COMMENT ON TABLE users IS 'Table stockant les informations d''authentification et de rôle des utilisateurs (clients/admins).';
COMMENT ON COLUMN users.email IS 'Adresse email unique servant d''identifiant de connexion.';
COMMENT ON COLUMN users.password IS 'Hash du mot de passe (jamais stocké en clair).';
COMMENT ON COLUMN users.role IS 'Rôle de l''utilisateur (ex: ROLE_USER, ROLE_ADMIN) pour Spring Security.';

-- =====================================================================
-- TABLE : products
-- =====================================================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_products_stock_quantity CHECK (stock_quantity >= 0)
);

COMMENT ON TABLE products IS 'Catalogue des produits disponibles à la vente.';
COMMENT ON COLUMN products.price IS 'Prix courant du produit. Typé en DECIMAL(10,2) pour garantir la précision financière stricte (pas de float/double).';
COMMENT ON COLUMN products.stock_quantity IS 'Quantité disponible en stock. La contrainte CHECK empêche mathématiquement les stocks négatifs en base.';

-- =====================================================================
-- TABLE : cart_items
-- =====================================================================
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Index composite pour optimiser la recherche, la vérification et la mise à jour des paniers
CREATE INDEX idx_cart_items_user_product ON cart_items (user_id, product_id);

COMMENT ON TABLE cart_items Ligne du panier d''achat temporaire d''un utilisateur.';
COMMENT ON COLUMN cart_items.quantity IS 'Quantité du produit dans le panier. Doit être strictement supérieure à 0.';

-- =====================================================================
-- TABLE : orders
-- =====================================================================
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    -- ON DELETE RESTRICT pour protéger l'intégrité financière et historique : on ne peut pas supprimer un utilisateur ayant des commandes.
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Index pour accélérer le chargement de l'historique de commandes par utilisateur
CREATE INDEX idx_orders_user_id ON orders (user_id);

COMMENT ON TABLE orders IS 'Historique des commandes passées. Représente une transaction financière validée ou en cours.';
COMMENT ON COLUMN orders.total_amount IS 'Montant total de la commande. Typé en DECIMAL(10,2) pour la précision financière.';
COMMENT ON COLUMN orders.status IS 'État de la commande (ex: PENDING, PAID, FAILED, SHIPPED).';
COMMENT ON COLUMN orders.payment_method IS 'Méthode de paiement utilisée (ex: CREDIT_CARD, MOBILE_MONEY, CASH_ON_DELIVERY).';

-- =====================================================================
-- TABLE : order_items
-- =====================================================================
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID,
    quantity INTEGER NOT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    -- ON DELETE SET NULL : Si un produit est supprimé du catalogue, on conserve l'historique de la commande mais on perd la référence produit.
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Index pour accélérer le chargement des détails d'une commande spécifique
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

COMMENT ON TABLE order_items Ligne de détail d''une commande (produits achetés).';
COMMENT ON COLUMN order_items.price_at_purchase IS 'RÈGLE CRITIQUE : Fige le prix du produit au moment exact de la commande. Protège l''historique financier contre les futures modifications du catalogue.';
COMMENT ON COLUMN order_items.product_id IS 'Référence au produit. Nullable (SET NULL) en cas de suppression du produit du catalogue pour préserver l''historique.';