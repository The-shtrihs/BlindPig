package ua.kpi.blindpig.catalog.service;

import ua.kpi.blindpig.catalog.dto.*;
import ua.kpi.blindpig.common.PageResult;

public interface MenuItemService {
    MenuItemDto create(SaveMenuItemRequest request);
    MenuItemDto getById(Long menuItemId);
    /** Same DTO; an unavailable item is rejected (409). */
    MenuItemDto getPurchasableItem(Long menuItemId);
    PageResult<MenuItemDto> search(MenuItemSearchCriteria criteria);
    MenuItemDto update(Long menuItemId, SaveMenuItemRequest request);
    /** JSON Patch and Merge Patch are applied/validated at web layer, then call update(). */
    void delete(Long menuItemId);
}
