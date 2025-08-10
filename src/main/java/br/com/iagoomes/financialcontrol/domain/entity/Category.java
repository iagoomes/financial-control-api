package br.com.iagoomes.financialcontrol.domain.entity;

import java.time.LocalDateTime;

/**
 * Category domain entity
 */
public class Category {

    private String id;
    private String name;
    private String color;
    private String icon;
    private String parentCategoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /**
     * Factory method to create a new category
     */
    public static Category create(String name, String color, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setColor(color);
        category.setIcon(icon);
        return category;
    }

    /**
     * Factory method to create a subcategory
     */
    public static Category createSubcategory(String name, String color, String icon, String parentCategoryId) {
        Category category = create(name, color, icon);
        category.setParentCategoryId(parentCategoryId);
        return category;
    }

    /**
     * Check if this is a root category (no parent)
     */
    public boolean isRootCategory() {
        return parentCategoryId == null || parentCategoryId.trim().isEmpty();
    }

    /**
     * Check if this is a subcategory
     */
    public boolean isSubcategory() {
        return !isRootCategory();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}