package com.mstock.api.payload.Responde;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralResponde<T> {
    T data;
    String msg;
    @JsonIgnore
    int httpcode;
}
