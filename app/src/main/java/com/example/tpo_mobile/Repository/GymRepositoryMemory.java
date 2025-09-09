package com.example.tpo_mobile.Repository;

import com.example.tpo_mobile.model.Clase;
import com.example.tpo_mobile.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GymRepositoryMemory {
    private final List<Clase> clases;
    private final List<User> users;

    @Inject
    public GymRepositoryMemory() {
        this.clases = new ArrayList<>();
        initializeClases();
        this.users = new ArrayList<>();
        initializeUsers();
    }

    private void initializeClases() {
        clases.add(new Clase("Pilates","Eduardo"));
        clases.add(new Clase("Box","Juan"));
        clases.add(new Clase("Zumba","Erika"));
        clases.add(new Clase("Pesas","Maria"));

    }



    private void  initializeUsers() {
        users.add(new User("dggtn@gmail.com","Daniela"));
        users.add(new User("juan@gmail.com","Juan"));
        users.add(new User("maria@gmail.com","Maria"));
        users.add(new User("luciana@gmail.com","Luciana"));

    }



}
