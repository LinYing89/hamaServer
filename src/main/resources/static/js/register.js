function checkUser(){
	var password = $("#input-psd").val();
	var ensurePassword = $("#input-ensure-psd").val();
	if(password != ensurePassword){
		$("#input-ensure-psd").addClass("is-invalid");
		return false;
	}
	return true;
}