package com.example.demo.site;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.site.dto.CreateSiteRequest;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/sites")
@AllArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public Site createSite(@RequestBody CreateSiteRequest site) {
        return siteService.createSite(site);
    }

    @GetMapping
    public List<Site> getSites() {
        return siteService.getSites();
    }
}