package ch.zhaw.neurofleet.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Auth0UserDTO {
    private String user_id;
    private String email;
    private String name;
    private String given_name;
    private String family_name;
    private List<String> roles;
}
