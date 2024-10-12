package com.study.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MemoryUserRepository implements UserRepository {

    public final List<User> users = new ArrayList<>();

    @Override
    public void save(User member) {
        users.add(member);
    }

    @Override
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(user -> Objects.equals(user.getId(), id))
                .findAny();
    }

    @Override
    public List<User> findAll() {
        return users;
    }
}
