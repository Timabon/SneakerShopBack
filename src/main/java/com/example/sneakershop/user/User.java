package com.example.sneakershop.user;

import com.example.sneakershop.basket.Basket;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity                 //tells to create a table in db
@Table(name = "users") //creates table in the database with name "users"
@NoArgsConstructor     //creates empty constructor
@AllArgsConstructor    //creates all arguments constructor
@Getter               //self-explanatory
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Means that is gonna be primary key in db
    @Column(name = "user_id") //names the column in db
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "password")
    private String password;
    @Column(name = "e_mail",unique = true)
    private String email;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    @JoinColumn(name = "basket_id", referencedColumnName = "id", nullable = true)
    private Basket basket;

    public User(String name) {
        this.name = name;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
        basket.setUser(this);
    }
}
