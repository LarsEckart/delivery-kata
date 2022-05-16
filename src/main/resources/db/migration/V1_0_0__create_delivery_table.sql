CREATE SEQUENCE delivery_id_seq;

CREATE TABLE Delivery
(
    id               integer     NOT NULL DEFAULT nextval('delivery_id_seq') PRIMARY KEY,
    email            varchar(50) NOT NULL,
    phone            varchar(15),
    date_of_order    timestamp   NOT NULL,
    date_of_delivery timestamp,
    arrived          boolean,
    onTime           boolean,
    latitude         float       NOT NULL,
    longitude        float       NOT NULL
);
