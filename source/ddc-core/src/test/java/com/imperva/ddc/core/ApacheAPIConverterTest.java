package com.imperva.ddc.core;

import com.imperva.ddc.core.query.*;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by gabi.beyo on 06/07/2015.
 */
public class ApacheAPIConverterTest {
    private static final String DN = "CN=Gabi,CN=Users,OU=ITP";
    private ApacheAPIConverter converter;

    @Before
    public void setup() {
        converter = new ApacheAPIConverter();
    }

    @Test
    public void testToStringArray() {
        List<Field> fields = new ArrayList<>();
        Field fieldName = new Field();
        fieldName.setName("givenName");
        Field field2Location = new Field();
        field2Location.setName("l");
        fields.add(fieldName);
        fields.add(field2Location);

        String[] st = new ApacheAPIConverter().toStringArray(fields);
        assertTrue(st[0].equals("givenName"));
        assertTrue(st[1].equals("l"));
    }

    @Test
    public void testNullToStringArray() {
        String[] st = new ApacheAPIConverter().toStringArray(null);
        assertTrue(st[0].equals("*"));
    }

    @Test
    public void testToApacheSearchRequest() throws LdapException {
        QueryRequest search = mock(QueryRequest.class);
        Endpoint endpoint = mock(Endpoint.class);

        when(search.getTimeLimit()).thenReturn(1000);
        when(search.getSizeLimit()).thenReturn(1000);
        when(search.getSearchText()).thenReturn("(l=LTV)");
        when(search.getReferralsHandling()).thenReturn(ReferralsHandling.FOLLOW);
        when(search.getRequestedFields()).thenReturn(null);

        SearchRequest searchRequest = new ApacheAPIConverter().toSearchRequest(search, endpoint.getBaseSearchPath());

        assertTrue(searchRequest.getSizeLimit() == search.getSizeLimit());
        assertTrue(searchRequest.getTimeLimit() == search.getTimeLimit());
        assertTrue(searchRequest.getFilter().toString().equals(search.getSearchText()));
        assertTrue(searchRequest.isFollowReferrals());
    }

    @Test
    public void testToApacheSearchRequestPaged() throws LdapException {
        QueryRequest search = mock(QueryRequest.class);
        Endpoint endpoint = mock(Endpoint.class);
        when(search.getPageChunkSize()).thenReturn(1000);
        when(search.getTimeLimit()).thenReturn(1000);
        when(search.getSizeLimit()).thenReturn(1000);
        when(search.getSearchText()).thenReturn("(l=LTV)");
        when(search.getReferralsHandling()).thenReturn(ReferralsHandling.FOLLOW);
        when(search.isPaged()).thenReturn(true);
        when(search.getRequestedFields()).thenReturn(null);
        byte[] cookie = new byte[1];
        SearchRequest searchRequest = new ApacheAPIConverter().toSearchRequest(search, endpoint.getBaseSearchPath(), endpoint.getCookie());

        assertTrue(searchRequest.getControls().values().toArray()[0] instanceof PagedResults);
        assertTrue(((PagedResults) searchRequest.getControls().values().toArray()[0]).getSize() == search.getPageChunkSize());
    }

    @Test
    public void testToEntityResponse() {
        try {
            List<Entry> entries = new ArrayList<>();
            Entry entry = new DefaultEntry();
            entry.setDn(DN);
            entry.add("givenName", "gabi");
            entries.add(entry);

            List<Field> fields = new ArrayList<>();
            Field fieldName = new Field();
            fieldName.setName("givenName");
            fieldName.setType(FieldType.FIRST_NAME);
            fields.add(fieldName);

            List<EntityResponse> responses = new ApacheAPIConverter().toEntityResponse(entries, fields);

           assertTrue(((ArrayList<Field>)responses.get(0).getValue()).get(0).getType() == FieldType.FIRST_NAME);
        } catch (LdapException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testToModificationRemoveAttributeWithValue() {
        final String value = "some group";
        final Field field = new Field(FieldType.GROUP, value);
        field.setName("memberOf");
        final RemoveModificationDetails details = new RemoveModificationDetails(DN, field);

        final Modification modification = converter.toModification(details);
        assertEquals("Unexpected operation", ModificationOperation.REMOVE_ATTRIBUTE, modification.getOperation());
        assertTrue("Expected attribute value", modification.getAttribute().contains(value));
    }

    @Test
    public void testToModificationRemoveAttributeNoValue() {
        final Field field = new Field();
        field.setType(FieldType.GROUP);
        field.setName("memberOf");
        final RemoveModificationDetails details = new RemoveModificationDetails(DN, field);

        final Modification modification = converter.toModification(details);
        assertEquals("Unexpected operation", ModificationOperation.REMOVE_ATTRIBUTE, modification.getOperation());
        assertEquals("Unexpected values", 0, modification.getAttribute().size());
    }
}
