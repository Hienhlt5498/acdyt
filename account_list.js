var protocol = window.location.protocol;
var host = window.location.host;
var ROOT_URL = protocol + '//' + host + CONTEXT_PATH;
// Vue
var app = new Vue({
	el: '#app',
	data: {
		siteName: 'VNZZZ Cloud Management',
		authUrl: '',
		cloudUrl: '',
		contentLanguage: 'en_US',
		accounts: [],
		hasAccountCreatePermission: false,
	},
	computed: {
		datatablesLangUrl: function () {
			if (this.contentLanguage == 'vi_VN') {
				return "//cdn.datatables.net/plug-ins/9dcbecd42ad/i18n/Vietnamese.json";
			}
			return "//cdn.datatables.net/plug-ins/1.10.21/i18n/English.json";
		}
	},
	created: function () {
	},
	mounted: function () {
		var self = this;
		Vue.axios.get(
			ROOT_URL + 'common', '',
			{
				headers: {
				}
			}
		).then(function (response) {
			self.siteName = response.data.data.site_name;
			self.authUrl = response.data.data.auth_url;
			self.cloudUrl = response.data.data.cloud_url;
			self.contentLanguage = response.data.data.content_language;
			document.title += ' | ' + self.siteName;

			self.getAccountList();
		});

		if ($('#account_create').val() !== undefined) {
			self.hasAccountCreatePermission = true;
		}
	},
	methods: {
		gotoAccountDetail: function (event, id) {
			event.preventDefault();
			localStorage.currentAccountId = id;
			document.location.href = ROOT_URL + 'account/detail';

		},
		gotoVMList: function (event, id) {
			event.preventDefault();
			localStorage.currentAccountId = id;
			document.location.href = ROOT_URL + 'resources';

		},
		getPermissionLink: function (id) {
			return ROOT_URL + 'account/' + id + '/permissions';
		},
		getAccountList: function () {
			var self = this;
			Vue.axios.get(
				self.authUrl + '/accounts', '', {}
			).then(function (response) {
				self.accounts = response.data.data.accounts;
				setTimeout(function () {
					if (self.hasAccountCreatePermission) {
						$('#accounts').DataTable({
							"language": {
								"url": self.datatablesLangUrl
							},
							"scrollX": true,
							"dom": "<'row'<'col-sm-4 col-md-4'l><'col-sm-4 col-md-6'f><'#toolbar.col-sm-4 col-md-2 dataTables_info pt-0'>>" +
								"<'row'<'col-sm-12'tr>>" +
								"<'row'<'col-sm-5 col-md-5'i><'col-sm-7 col-md-7'p>>",
							"initComplete": function (settings, json) {
								if (self.contentLanguage == 'vi_VN') {
									$('div#toolbar').html('<button id="newAccountBtn" class="btn float-sm-right btn-custom-primary"><i class="fa fa-plus"></i><span> Tạo mới</span></button>')
								} else {
									$('div#toolbar').html('<button id="newAccountBtn" class="btn float-sm-right btn-custom-primary"><i class="fa fa-plus"></i><span> Add New</span></button>')
								}
								$('#newAccountBtn').on('click', function () {
									document.location.href = ROOT_URL + 'account/add';
								});
								$('#accounts').attr('style', 'border-collapse: collapse !important; width: 100%');
							}
						});
					} else {
						$('#accounts').DataTable({
							"language": {
								"url": self.datatablesLangUrl
							},
							"scrollX": true,
						});
					}
				}, 1);
			}).catch(function (error) {
				if (error.response.status == 400) {
					let errorMessage = error.response.data.errors[0].message;
					showErrorToastr(errorMessage);
				}
			});
		},
		gotoBillDetail: function (event, id, name) {
			event.preventDefault();
			localStorage.currentAccountId = id;
			localStorage.currentAccountName = name;
			document.location.href = ROOT_URL + 'account/bill';
		}
	}
});