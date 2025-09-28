package vn.thanhquan.controller.request;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter // no usages new *
@Setter
public class AddressRequest implements Serializable {
    // id bigserial NOT NULL,
    // apartment_number varchar(255) NULL,
    // floor varchar(255) NULL,
    // building varchar(255) NULL,
    // street_number varchar(255) NULL,
    // street varchar(255) NULL,
    // city varchar(255) NULL,
    // country varchar(255) NULL,
    // address_type int4 NULL,
    // user_id int8 NULL,
    // created_at timestamp(6) DEFAULT now() NULL,
    // updated_at timestamp(6) DEFAULT now() NULL,
    // CONSTRAINT tbl_address_pkey PRIMARY KEY (id)
    //

    private String apartmentNumber;
    private String floor;
    private String building;
    private String streetNumber;
    private String street;
    private String city;
    private String country;
    private Integer addressType;

}
