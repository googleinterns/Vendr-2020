/**
 * This is only for testing purposes (will be deleted).
 * This is the expected output from the servlet.
 */
const vendor = {
    id: 1,
    firstName: 'Abraham',
    lastName: 'Haros',
    email: 'abrahamharos',
    phoneNumber: '8181784521',
    saleCard:
    {
        id: 1,
        businessName: 'Sweet Taco',
        description: 'We sell sweet tacos',
        hasDelivery: true,
        startTime: {
            hour: 07,
            minute: 30,
            second: 0,
            nano: 0
        },
        endTime: {
            hour: 17,
            minute: 30,
            second: 0,
            nano: 0
        },
        location: {
            id: 1,
            salePoint: {
                latitude: 29.103398,
                longitude: -111.007043
            },
            geoHash: 'abcdefg',
            radius: 1000,
        },
        picture: {
            id: 1,
            blobKey: {
                blobKey: 'QU2duv7QZJrMc6SQh_NcBw'
            },
            altText: 'Not a taco'
        },
        distanceFromClient: 950
    }
};