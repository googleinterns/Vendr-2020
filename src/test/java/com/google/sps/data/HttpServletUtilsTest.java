package com.google.sps.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.time.LocalTime;
import static com.google.sps.utility.NearbyVendorsQueryTest.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public final class HttpServletUtilsTest {
    // Mock Http objects.
    private static HttpServletRequest mockedRequest;
    private static HttpServletResponse mockedResponse;

    // Request parameters names.
    private static final String PARAM_LATITUDE = "lat";
    private static final String PARAM_LONGITUDE = "lng";

    // Default values declaration.
    private static final String DEFAULT_PARAM_LATITUDE = "0";
    private static final String DEFAULT_PARAM_LONGITUDE = "0";

    // Mock Places.
    private final GeoPt PLACE_INITIAL = new GeoPt(0, 0);

    // Test Strings.
    private final String STRING_ONLY_LETTERS = "ThisStringOnlyHasLetters";
    private final String STRING_WITH_SPACE = "This String Contains Spaces";
    private final String STRING_WITH_SPECIAL_CHARACTERS = "12#@ $FDGD @#$--0=93";
    private final String STRING_EMPTY = "";
    private final String STRING_NULL = null;
    private final String STRING_ONLY_NUMBERS = "1231231231";

    // Time values.
    private static final LocalTime TIME_0000 = LocalTime.parse("00:00");
    private static final LocalTime TIME_0800 = LocalTime.parse("08:00");

    // Location Data values.
    private static final LocationData LOC_50_M_FROM_NL = createLocation(1, 0.0003179f);

    // SaleCard values.
    private static final SaleCard SCARD_50M =
            createSaleCard(1, "A", false, false, TIME_0000, TIME_0800, LOC_50_M_FROM_NL);

    // Vendor values.
    private static final String VENDOR_ID = "1";
    private static final String VENDOR_WRONG_ID = "2";
    private static final Vendor VENDOR_50M = new Vendor(VENDOR_ID, "Vendor", "A", null, null, null, SCARD_50M);

    // Mock datastore service.
    private static final LocalServiceTestHelper datastoreHelper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    // Class instance.
    HttpServletUtils httpServletUtils;

    @BeforeClass
    public static void setUpDatastore() {
        datastoreHelper.setUp();
        fillDatastore();
    }

    @AfterClass
    public static void tearDownDatastore() {
        datastoreHelper.tearDown();
    }

    @Before
    public void setUp() {
        mockedRequest = mock(HttpServletRequest.class);
        mockedResponse = mock(HttpServletResponse.class);
        when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(String.valueOf(PLACE_INITIAL.getLatitude()));
        when(mockedRequest.getParameter(PARAM_LONGITUDE)).thenReturn(String.valueOf(PLACE_INITIAL.getLongitude()));
    }

    // Get parameter content when the parameter is not null.
    @Test
    public void getParameterFromRequest() {
        Assert.assertEquals(String.valueOf(PLACE_INITIAL.getLatitude()),
                httpServletUtils.getParameter(mockedRequest, PARAM_LATITUDE, DEFAULT_PARAM_LATITUDE));
    }

    // Get parameter content when the parameter is null.
    @Test
    public void getParameterFromRequestNullParameter() {
        // Override value and set it to null.
        when(mockedRequest.getParameter(PARAM_LATITUDE)).thenReturn(null);

        Assert.assertEquals(DEFAULT_PARAM_LATITUDE,
                httpServletUtils.getParameter(mockedRequest, PARAM_LATITUDE, DEFAULT_PARAM_LATITUDE));
    }

    // Get parameter content when request is null.
    @Test
    public void getParameterFromNullRequest() {
        Assert.assertEquals(DEFAULT_PARAM_LATITUDE,
                httpServletUtils.getParameter(null, PARAM_LATITUDE, DEFAULT_PARAM_LATITUDE));
    }

    // Get parameter content when the parameter is null.
    @Test
    public void getParameterWhenNotExists() {
        Assert.assertEquals(DEFAULT_PARAM_LATITUDE,
                httpServletUtils.getParameter(mockedRequest, null, DEFAULT_PARAM_LATITUDE));
    }

    // Verify that the given String only contain letters
    @Test
    public void hasOnlyLetters() {
        Assert.assertTrue(httpServletUtils.hasOnlyLetters(STRING_ONLY_LETTERS));
    }

    // Verify that given a String with spaces, the method returns false.
    @Test
    public void stringWithSpaces() {
        Assert.assertFalse(httpServletUtils.hasOnlyLetters(STRING_WITH_SPACE));
    }

    // Verify that given a String with special characters, the method returns false.
    @Test
    public void stringWithSpecialCharacters() {
        Assert.assertFalse(httpServletUtils.hasOnlyLetters(STRING_WITH_SPECIAL_CHARACTERS));
    }

    // Verify that given an empty String, the method returns false.
    @Test
    public void emptyString() {
        Assert.assertFalse(httpServletUtils.hasOnlyLetters(STRING_EMPTY));
    }

    // Verify that given a null String, the method returns false.
    @Test
    public void nullString() {
        Assert.assertFalse(httpServletUtils.hasOnlyLetters(STRING_NULL));
    }

    // Verify if a String only contains numbers.
    @Test
    public void hasOnlyNumbers() {
        Assert.assertTrue(httpServletUtils.hasOnlyNumbers(STRING_ONLY_NUMBERS));
    }

    // Retrieve the correct vendor from datastore.
    @Test
    public void retrieveVendorFromDatastore() {
        Entity vendorExpected = createEntityFromVendor(VENDOR_50M);
        Assert.assertEquals(vendorExpected, httpServletUtils.getVendorEntity(VENDOR_ID));
    }

    // Retrieve null value if vendor does not exists on datastore.
    @Test
    public void retrieveNullVendorFromDatastore() {
        Assert.assertEquals(null, httpServletUtils.getVendorEntity(VENDOR_WRONG_ID));
    }

    // Compute Geo Distance given two GeoPoints with 1 meter of difference allowed.
    @Test
    public void computeGeoDistanceMeterDelta() {
        Assert.assertEquals(50f,
                httpServletUtils.computeGeoDistance(
                        PLACE_INITIAL, VENDOR_50M.getSaleCard().getLocation().getSalePoint()),
                1f // Delta
        );
    }

    // Compute Geo Distance given two GeoPoints with 1 meter of difference allowed.
    @Test
    public void computeGeoDistanceCentimetersDelta() {
        Assert.assertEquals(50f,
                httpServletUtils.computeGeoDistance(
                        PLACE_INITIAL, VENDOR_50M.getSaleCard().getLocation().getSalePoint()),
                0.01f // Delta
        );
    }

    // Create embedded entities correctly.
    @Test
    public void createEmbeddedEntity() {
        // Expected embedded entity construction
        Entity vendorEntity = createEntityFromVendor(VENDOR_50M);
        EmbeddedEntity embeddedEntityExpected = new EmbeddedEntity();
        embeddedEntityExpected.setKey(vendorEntity.getKey());
        embeddedEntityExpected.setPropertiesFrom(vendorEntity);

        Assert.assertEquals(embeddedEntityExpected, httpServletUtils.createEmbeddedEntity(vendorEntity));
    }

    private static void fillDatastore() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(createEntityFromVendor(VENDOR_50M));
    }
}