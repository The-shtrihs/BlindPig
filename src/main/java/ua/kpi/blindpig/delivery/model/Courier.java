package ua.kpi.blindpig.delivery.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Courier {
    private Long id;
    private String name;
    private String phone;
    private boolean available;

    public Courier(String name, String phone, boolean available) {
        this(null, name, phone, available);
    }
}
