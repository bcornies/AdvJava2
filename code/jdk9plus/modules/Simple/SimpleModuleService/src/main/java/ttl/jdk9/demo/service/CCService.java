package ttl.jdk9.demo.service;

import ttl.jdk9.demo.service.impl.CCServiceImpl;

public interface CCService {
    public boolean hasCredit(String custId);

    public static CCService create() {
        return new CCServiceImpl();
    }
}
