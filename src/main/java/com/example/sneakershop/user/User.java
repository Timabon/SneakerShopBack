package com.example.sneakershop.user;

import com.example.sneakershop.basket.Basket;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "name shouldn't be null")
    @Size(min = 2, max = 15)
    @Column(name = "name")
    private String name;
    @NotBlank(message = "password cannot be blank")
    @Column(name = "password")
    private String password;
    @Email
    @NotBlank(message = "email cannot be blank")
    @Column(name = "e_mail",unique = true)
    private String email;

    @JsonIgnore
    @JsonBackReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "basket_id", referencedColumnName = "id")
    private Basket basket;

    public User(String name) {
        this.name = name;
    }


}
