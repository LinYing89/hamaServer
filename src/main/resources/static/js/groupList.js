$(document).ready(function() {
	
	$(".del-group").click(function() {
		var r = confirm("确认删除设备组吗?");
		if (r == true) {
			var url = $(this).attr("href");
			window.location.href=url;
		} 
		return false;
	});
});

$('#editGroupModal').on('show.bs.modal', function(event) {
	var modal = $(this)
	var target = $(event.relatedTarget) // Button that triggered the modal
	var title = modal.find('#editGroupModalTitle');
	if (target.data('option') == 'add') {
		title.text("添加设备组");
		var userId = target.data('user-id')
		modal.find('form').attr('action', '/group/add/' + userId)
	} else {
		title.text("编辑设备组");
		var groupId = target.data('group-id')
		var groupName = target.data('group-name') // Extract info from data-*
		var groupPetName = target.data('group-petname')
		var password = target.data('group-password')
		
		modal.find('#group-name').val(groupName)
		modal.find('#group-petname').val(groupPetName)
		modal.find('#group-password').val(password)
		modal.find('#group-ensurepassword').val(password)
		modal.find('form').attr('action', '/group/edit/' + groupId)
	}
});

function checkGroupForm(){
	var password = $('#group-password').val();
	var ensurePassword = $('#group-ensurepassword').val();
	if(password != ensurePassword){
		return false;
	}
	return true;
}