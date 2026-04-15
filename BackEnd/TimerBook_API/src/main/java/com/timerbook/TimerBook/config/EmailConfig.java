package com.timerbook.TimerBook.config;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private Boolean ssL;

    public EmailConfig() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSsL() {
        return ssL;
    }

    public void setSsL(Boolean ssL) {
        this.ssL = ssL;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EmailConfig that)) return false;
        return port == that.port && Objects.equals(host, that.host) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(ssL, that.ssL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, password, ssL);
    }
}
