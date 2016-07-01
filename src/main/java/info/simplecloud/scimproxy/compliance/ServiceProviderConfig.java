package info.simplecloud.scimproxy.compliance;

import info.simplecloud.scimproxy.compliance.enteties.AuthMetod;

import java.util.ArrayList;
import java.util.List;

public class ServiceProviderConfig {

    private boolean         patch                 = false;
    private boolean         bulk                  = false;
    private int             bulkMaxOperations     = 0;
    private int             bulkMaxPayloadSize    = 0;
    private boolean         filter                = false;
    private int             filderMaxResults      = 0;
    private boolean         changePassword        = false;
    private boolean         sort                  = false;
    private boolean         etag                  = false;
    private boolean         xmlDataFormat         = false;
    private List<AuthMetod> authenticationSchemes = new ArrayList<AuthMetod>();

    public void setPatch(boolean patch) {
        this.patch = patch;
    }

    public boolean hasPatch() {
        return patch;
    }

    public void setBulk(boolean bulk) {
        this.bulk = bulk;
    }

    public boolean hasBulk() {
        return bulk;
    }

    public void setBulkMaxOperations(int bulkMaxOperations) {
        this.bulkMaxOperations = bulkMaxOperations;
    }

    public int getBulkMaxOperations() {
        return bulkMaxOperations;
    }

    public void setBulkMaxPayloadSize(int bulkMaxPayloadSize) {
        this.bulkMaxPayloadSize = bulkMaxPayloadSize;
    }

    public int getBulkMaxPayloadSize() {
        return bulkMaxPayloadSize;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public boolean hasFilter() {
        return filter;
    }

    public void setFilderMaxResults(int filderMaxResults) {
        this.filderMaxResults = filderMaxResults;
    }

    public int getFilderMaxResults() {
        return filderMaxResults;
    }

    public void setChangePassword(boolean changePassword) {
        this.changePassword = changePassword;
    }

    public boolean hasChangePassword() {
        return changePassword;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public boolean hasSort() {
        return sort;
    }

    public void setEtag(boolean etag) {
        this.etag = etag;
    }

    public boolean hasEtag() {
        return etag;
    }

    public void setXmlDataFormat(boolean xmlDataFormat) {
        this.xmlDataFormat = xmlDataFormat;
    }

    public boolean hasXmlDataFormat() {
        return xmlDataFormat;
    }

    public void addAuthenticationScheme(AuthMetod authenticationScheme) {
        this.authenticationSchemes.add(authenticationScheme);
    }

    public List<AuthMetod> getAuthenticationSchemes() {
        return authenticationSchemes;
    }
}
