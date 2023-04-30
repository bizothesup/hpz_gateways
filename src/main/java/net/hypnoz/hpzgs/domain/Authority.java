package net.hypnoz.hpzgs.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Table("authority")
public class Authority implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;


    @NotNull
    @Size(max = 50)
    @Id
    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getId() {
        return name;
    }
    @Override
    public boolean isNew() {
        return true;
    }
}
