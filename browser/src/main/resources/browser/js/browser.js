var browser = angular.module('browser', [ 'ngRoute', 'browserControllers',
		'jsonFormatter' ]);

browser.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/facet', {
		templateUrl : 'partials/facet-ruleforms.html',
		controller : 'FacetRuleformsListCtrl'
	}).when('/facet/:ruleform', {
		templateUrl : 'partials/facets.html',
		controller : 'FacetListCtrl'
	}).when('/facet/:ruleform/:classifier/:classification', {
		templateUrl : 'partials/facet-instances.html',
		controller : 'FacetInstancesListCtrl'
	}).when('/facet/:ruleform/:classifier/:classification/:instance', {
		templateUrl : 'partials/instance-detail.html',
		controller : 'FacetInstanceDetailCtrl'
	}).when('/ruleform', {
		templateUrl : 'partials/ruleforms.html',
		controller : 'RuleformListCtrl'
	}).when('/ruleform/:ruleform', {
		templateUrl : 'partials/ruleform-instances.html',
		controller : 'RuleformInstancesListCtrl'
	}).when('/ruleform/:ruleform/:instance', {
		templateUrl : 'partials/instance-detail.html',
		controller : 'RuleformInstanceDetailCtrl'
	});
	$routeProvider.otherwise({
		redirectTo : '/facet'
	});
} ]);