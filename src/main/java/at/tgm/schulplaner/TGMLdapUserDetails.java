package at.tgm.schulplaner;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

/**
 * @author Georg Burkl
 * @version 2020-03-11
 */
public class TGMLdapUserDetails extends LdapUserDetailsImpl {
    private String name;
    private String employeeType;
    private String department;

    protected TGMLdapUserDetails() {

    }

    public String getName() {
        return name;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public String getDepartment() {
        return department;
    }

    public static class Essence extends LdapUserDetailsImpl.Essence {
        public Essence() {
            super();
        }

        public Essence(DirContextOperations ctx) {
            super(ctx);
        }

        public Essence(TGMLdapUserDetails copyMe) {
            super(copyMe);
        }

        @Override
        protected LdapUserDetailsImpl createTarget() {
            return new TGMLdapUserDetails();
        }

        public void setName(String name) {
            ((TGMLdapUserDetails) instance).name = name;
        }

        public void setEmployeeType(String employeeType) {
            ((TGMLdapUserDetails) instance).employeeType = employeeType;
        }

        public void setDepartment(String department) {
            ((TGMLdapUserDetails) instance).department = department;
        }

    }
}
