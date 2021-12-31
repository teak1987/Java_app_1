$(document).ready(function() {
    $("#submit").on("click", function() {
    	$("#submit").prop("disabled", true);
    	var name = $("#name").val();
	    var address = $("#address").val();
        var file = $("#image").val(); 
        var form = $("#form").serialize();
    	var data = new FormData($("#form")[0]);
    	data.append('name', name);
    	data.append('address', address);
        data.append("file",file);
    	//alert(data);
       if (name === "" ||  address === "" || file === "" ) {
        	$("#submit").prop("disabled", false);
            $("#error_name").html("Name field is required.").delay(2000).fadeOut('slow');
            $("#error_address").html("Address field is required.").delay(2000).fadeOut('slow');
            $("#error_file").html("File field is required.").delay(2000).fadeOut('slow');
        } else {
            $('#error_name').css('opacity', 0);
            $('#error_address').css('opacity', 0);
            $('#error_file').css('opacity', 0);
                    $.ajax({
                        type: 'POST',
                        enctype: 'multipart/form-data',
                        data: data,
                        url: "/contact/create", 
                        processData: false,
                        contentType: false,
                        cache: false,
                        success: function(data, statusText, xhr) {
                        console.log(xhr.status);
                        if(xhr.status == "200") {
                        	$("#form")[0].reset();
                        	$('#success').css('display','block');
                            $("#error").text("");
	                       $("#submit").prop("disabled", false);
                            $("#success").html("Contact Inserted Succsessfully.");
                            $('#success').delay(2000).fadeOut('slow');
                         }	   
                        },
                        error: function(e) {
                        	$('#error').css('display','block');
                            $("#error").html("Oops! something went wrong.");
                            $('#error').delay(5000).fadeOut('slow');
                            location.reload();
                        }
                    });
        }
            });
       });