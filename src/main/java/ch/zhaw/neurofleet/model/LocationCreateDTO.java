package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LocationCreateDTO {
    private String name;
    private String address;
    private String companyId;
}
