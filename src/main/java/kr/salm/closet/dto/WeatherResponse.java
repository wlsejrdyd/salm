package kr.salm.closet.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    private String city;
    private int temperature;
    private int feelsLike;
    private String condition;
    private String icon;
    private int humidity;
    private double windSpeed;
    private String recommendation;
}
