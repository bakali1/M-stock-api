package com.mstock.api.payload.Request;

import com.mstock.api.Enum.BatchStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchSearchRequest {
    private String location;
    private String lotNumber;
    private String productName;
    private String productnsn;
    private BatchStatusEnum status;
}
