package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a response object for the api https://docs.disify.com/
 * 
 * For informations about the properties see:
 * https://docs.disify.com/?java#json-response-parameters
 **/
@Getter
@Setter
@NoArgsConstructor
public class MailInformation {
    private boolean format;
    private boolean alias;
    private String domain;
    private boolean disposable;
    private boolean dns;
}