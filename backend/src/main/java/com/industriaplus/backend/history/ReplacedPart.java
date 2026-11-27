package com.industriaplus.backend.history;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "replaced_parts")
@Getter
@NoArgsConstructor
public class ReplacedPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "history_id", nullable = false)
    private HistoryEntry history;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    public ReplacedPart(HistoryEntry history, String name, Integer quantity) {
        this.history = history;
        this.name = name;
        this.quantity = quantity;
    }
}
