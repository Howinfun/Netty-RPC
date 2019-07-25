package com.hyf.rpc.zookeeper.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/25
 */
@Data
@Builder
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IPPojo {
    private String ip;
    private Integer port;
}
