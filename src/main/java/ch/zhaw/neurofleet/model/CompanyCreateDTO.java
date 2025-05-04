package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CompanyCreateDTO {
    private String name;
    private String email;
    private String address;
    private String owner;

}
