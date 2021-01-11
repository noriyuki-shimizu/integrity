package com.example.integrity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Confirm {
    private static final String URL = "jdbc:mysql://localhost:3307/pokemon?serverTimezone=JST";
    private static final String USERNAME = "pokemon";
    private static final String PASSWORD = "pokemon";

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class Pokemon {
        private Integer id;
        private Integer height;
        private Integer weight;
        private Integer order;
        private String imageColor;
        private Integer regionId;
        private Integer pokemonEvolutionId;
    }

    /**
     * 実行
     */
    public void exec() {
        final List<Pokemon> jsonData = getJsonData();
        final List<Pokemon> dbData = getDbData();

        List<Pokemon> notMatchPokemons = jsonData.stream().filter(actual -> {
            Optional<Pokemon> first = dbData.stream().filter(data -> actual.getId().equals(data.getId())).findFirst();
            if (first.isEmpty()) {
                return true;
            }
            Pokemon pokemon = first.get();
            return !pokemon.getHeight().equals(actual.getHeight()) ||
                    !pokemon.getImageColor().equals(actual.getImageColor()) ||
                    !pokemon.getOrder().equals(actual.getOrder()) ||
                    !pokemon.getRegionId().equals(actual.getRegionId()) ||
                    !pokemon.getWeight().equals(actual.getWeight());

        }).collect(Collectors.toList());

        if (notMatchPokemons.size() > 0) {
            System.out.println("一致していないデータがあります");
            notMatchPokemons.forEach(System.out::println);
            return;
        }
        System.out.println("全てのデータが一致しています");
    }

    /**
     * DBに接続して、格納されているデータを取得します.
     *
     * @return DB データリスト
     */
    private List<Pokemon> getDbData() {
        try (Connection conn =
                     DriverManager.getConnection(URL, USERNAME, PASSWORD);
             // TODO 接続するテーブルの指定を汎用的にしたい
             PreparedStatement ps = conn.prepareStatement("select * from pokemons")) {

            final List<Pokemon> pokemons = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pokemons.add(
                            new Pokemon(
                                    rs.getInt("id"),
                                    rs.getInt("height"),
                                    rs.getInt("weight"),
                                    rs.getInt("order"),
                                    rs.getString("image_color"),
                                    rs.getInt("region_id"),
                                    null
                            )
                    );
                }
            }
            return pokemons;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * JSON ファイルからデータを読み取ります.
     * @return JSON データリスト
     */
    private List<Pokemon> getJsonData() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // TODO JSONファイルの指定を汎用的にしたい
            return mapper.readValue(ResourceUtils.getFile("classpath:pokemons.json"), new TypeReference<List<Pokemon>>() {});
        } catch (JsonProcessingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}