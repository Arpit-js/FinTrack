package com.fynance;

import java.util.Objects;

public class Category {

    private int categoryId;
    private String name;

    public Category(int categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Override toString() to display nicely in JComboBox
    @Override
    public String toString() {
        return name;
    }

    // Override equals and hashCode for proper JComboBox selection
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Category category = (Category) o;
        return categoryId == category.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }
}
