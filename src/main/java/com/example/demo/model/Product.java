package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    @Lob
    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String description;
    private String image;
    @Column(name = "image_path")
    private String imagePath;
    @Column(name = "stock")
    private Integer stock;
    @Column(name = "discount")
    private Integer discount;
    @Column(name = "original_price")
    private Double originalPrice;
    @Column(name = "promotion")
    private String promotion;
    @Column(name = "promotion_stock")
    private Integer promotionStock;
    @Column(name = "start_promotion_stock")
    private Integer startPromotionStock;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public String getFormattedPrice() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(getEffectivePrice()) + "đ";
    }

    public double getEffectivePrice() {
        if (promotionStock != null && promotionStock > 0) {
            return price;
        }
        return (originalPrice != null) ? originalPrice : price;
    }

    public double calculateTotalForQuantity(int requestedQty) {
        if (promotionStock == null || promotionStock <= 0) {
            return requestedQty * ((originalPrice != null) ? originalPrice : price);
        }
        
        int promoQty = Math.min(requestedQty, promotionStock);
        int regularQty = requestedQty - promoQty;
        
        double promoTotal = promoQty * price;
        double regularPrice = (originalPrice != null) ? originalPrice : price;
        double regularTotal = regularQty * regularPrice;
        
        return promoTotal + regularTotal;
    }

    public boolean isPromotionSplit(int requestedQty) {
        return promotionStock != null && promotionStock > 0 && requestedQty > promotionStock;
    }
}