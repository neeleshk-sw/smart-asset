package com.smartasset.search.controller;

import com.smartasset.search.service.SearchService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "assets") String index) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(query, index)));
    }

    @PostMapping("/index/{index}")
    public ResponseEntity<ApiResponse<Void>> index(
            @PathVariable String index,
            @RequestParam String id,
            @RequestBody Map<String, Object> document) {
        searchService.index(index, id, document);
        return ResponseEntity.ok(ApiResponse.success("Document indexed", null));
    }
}
