(function() {
  'use strict';

  var context = {
    '@context':    {
        '@vocab': 'http://schema.org/',
        'acceptsReservations': { '@type': '@id' },
        'additionalType': { '@type': '@id' },
        'applicationCategory': { '@type': '@id' },
        'applicationSubCategory': { '@type': '@id' },
        'arrivalTime': { '@type': 'DateTime' },
        'artform': { '@type': '@id' },
        'availabilityEnds': { '@type': 'DateTime' },
        'availabilityStarts': { '@type': 'DateTime' },
        'availableFrom': { '@type': 'DateTime' },
        'availableThrough': { '@type': 'DateTime' },
        'birthDate': { '@type': 'Date' },
        'bookingTime': { '@type': 'DateTime' },
        'checkinTime': { '@type': 'DateTime' },
        'checkoutTime': { '@type': 'DateTime' },
        'codeRepository': { '@type': '@id' },
        'commentTime': { '@type': 'Date' },
        'contentUrl': { '@type': '@id' },
        'dateCreated': { '@type': 'Date' },
        'dateIssued': { '@type': 'DateTime' },
        'dateModified': { '@type': 'Date' },
        'datePosted': { '@type': 'Date' },
        'datePublished': { '@type': 'Date' },
        'deathDate': { '@type': 'Date' },
        'departureTime': { '@type': 'DateTime' },
        'discussionUrl': { '@type': '@id' },
        'dissolutionDate': { '@type': 'Date' },
        'doorTime': { '@type': 'DateTime' },
        'downloadUrl': { '@type': '@id' },
        'dropoffTime': { '@type': 'DateTime' },
        'embedUrl': { '@type': '@id' },
        'endDate': { '@type': 'Date' },
        'endTime': { '@type': 'DateTime' },
        'expectedArrivalFrom': { '@type': 'DateTime' },
        'expectedArrivalUntil': { '@type': 'DateTime' },
        'expires': { '@type': 'Date' },
        'featureList': { '@type': '@id' },
        'foundingDate': { '@type': 'Date' },
        'gameLocation': { '@type': '@id' },
        'gamePlatform': { '@type': '@id' },
        'guidelineDate': { '@type': 'Date' },
        'hasMap': { '@type': '@id' },
        'image': { '@type': '@id' },
        'installUrl': { '@type': '@id' },
        'isBasedOnUrl': { '@type': '@id' },
        'labelDetails': { '@type': '@id' },
        'lastReviewed': { '@type': 'Date' },
        'license': { '@type': '@id' },
        'logo': { '@type': '@id' },
        'map': { '@type': '@id' },
        'maps': { '@type': '@id' },
        'material': { '@type': '@id' },
        'memoryRequirements': { '@type': '@id' },
        'menu': { '@type': '@id' },
        'modifiedTime': { '@type': 'DateTime' },
        'namedPosition': { '@type': '@id' },
        'orderDate': { '@type': 'DateTime' },
        'ownedFrom': { '@type': 'DateTime' },
        'ownedThrough': { '@type': 'DateTime' },
        'paymentDue': { '@type': 'DateTime' },
        'paymentUrl': { '@type': '@id' },
        'pickupTime': { '@type': 'DateTime' },
        'prescribingInfo': { '@type': '@id' },
        'previousStartDate': { '@type': 'Date' },
        'priceValidUntil': { '@type': 'Date' },
        'publishingPrinciples': { '@type': '@id' },
        'relatedLink': { '@type': '@id' },
        'releaseDate': { '@type': 'Date' },
        'releaseNotes': { '@type': '@id' },
        'replyToUrl': { '@type': '@id' },
        'requirements': { '@type': '@id' },
        'roleName': { '@type': '@id' },
        'sameAs': { '@type': '@id' },
        'scheduledPaymentDate': { '@type': 'Date' },
        'scheduledTime': { '@type': 'DateTime' },
        'screenshot': { '@type': '@id' },
        'serviceUrl': { '@type': '@id' },
        'significantLink': { '@type': '@id' },
        'significantLinks': { '@type': '@id' },
        'sport': { '@type': '@id' },
        'startDate': { '@type': 'Date' },
        'startTime': { '@type': 'DateTime' },
        'storageRequirements': { '@type': '@id' },
        'surface': { '@type': '@id' },
        'targetUrl': { '@type': '@id' },
        'temporal': { '@type': 'DateTime' },
        'thumbnailUrl': { '@type': '@id' },
        'ticketToken': { '@type': '@id' },
        'trackingUrl': { '@type': '@id' },
        'uploadDate': { '@type': 'Date' },
        'url': { '@type': '@id' },
        'validFrom': { '@type': 'DateTime' },
        'validThrough': { '@type': 'DateTime' },
        'validUntil': { '@type': 'Date' },
        'warning': { '@type': '@id' },
        'webCheckinTime': { '@type': 'DateTime' }
    }
  };

  angular
    .module('angularJsonld.schema.org', ['angularJsonld'])
    .config(config);

  /* @ngInject */
  function config(jsonldProvider, jsonldContextProvider){
    jsonldProvider.registerContext('http://schema.org', context);
    jsonldContextProvider.add({
      'schema': 'http://schema.org/'
    });
  }

})();
