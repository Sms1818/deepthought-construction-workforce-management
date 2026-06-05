package com.example.demo.site.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CreateSiteRequest {

    @NotBlank
    private String siteName;

    @NotBlank
    private String location;
}