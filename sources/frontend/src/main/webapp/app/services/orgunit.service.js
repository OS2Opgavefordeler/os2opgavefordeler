(function () {
	'use strict';

	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log', 'topicRouterClient'];

	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log, client) {
		var service = {
			getOrgUnits: getOrgUnits,
			getOrgUnitsAsTree: getOrgUnitsAsTree,
			getOrgUnit: getOrgUnit,
			addKle: addKle,
			removeKle: removeKle,
			getKles: getKles
		};

		return service;

		function getOrgUnits() {
			return client.get('/ou/list');
		}

		function getOrgUnitsAsTree() {
			return client.get('/ou');
		}

		function getOrgUnit(id) {
			return client.get('/ou/' + id);
		}

		function getKles() {
			return client.get('/kle/tree');
		}

		function addKle(kle, orgunit, assignment) {
			return client.post("/ou/" + orgunit.id + "/" + assignment + "/" + kle.number, null);
		}

		function removeKle(kle, orgunit, assignment) {
			return client.delete("/ou/" + orgunit.id + "/" + assignment + "/" + kle.number, null);
		}

		// function simulateRestCall(functionToSimulate) {
		// 	return $timeout(functionToSimulate, 10);
		// }

	}
})();