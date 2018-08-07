$(document).ready(function() {
	$(".btnEdit").click(function() {
		btnEditGroup(this);
		//阻止超链接
		return false;
	});
	$(".btnDel").click(function() {
		btnDelGroup(this);
		return false;
	});
	$("#btnModalDelGroup").click(function() {
		var groupId = $("#groupId").text();
		$(location).prop('href', '/group/delete/' + groupId);
	});
});

//编辑操作
function btnEditGroup(liGroup) {
	//获取元素title属性值, title属性值为组id
	var title=liGroup.getAttribute("title");
	console.info(title);
	$(location).prop('href', '/group/edit/' + title);
}

//删除操作
function btnDelGroup(liGroup) {
	var title=liGroup.getAttribute("title");
	console.info(title);
	$("#groupId").text(title);
	$('#deleteModal').modal('show')
}