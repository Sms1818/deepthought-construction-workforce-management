package com.example.demo.site;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.site.dto.CreateSiteRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    public Site createSite(CreateSiteRequest request) {
        Site site = Site.builder()
                .siteName(request.getSiteName())
                .location(request.getLocation())
                .active(true)
                .build();

        return siteRepository.save(site);
    }

    public List<Site> getSites() {
        return siteRepository.findAll();
    }
}