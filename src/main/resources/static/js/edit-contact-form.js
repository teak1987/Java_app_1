$(document).ready(function() {
	$("#submitEdit").on("click", function() {
	/*	alert("Hellooo");*/
		var id = $("#id").val();
    	var name = $("#name").val();
	    var address = $("#address").val();
        var file = $("#image").val(); 
        var form = $("#form").serialize();
        var data = new FormData($("#form")[0]);	 
		data.append('id',id);
		data.append('name', name);
		data.append('address', address);
	
		$.ajax({
			type: 'POST',
			enctype: 'multipart/form-data',
			data: data,
			url: "/all-contacts/edit/" + id,
			processData: false,
			contentType: false,
			cache: false,
			success: function(data, statusText, xhr) {
				console.log(xhr.status);
				if (xhr.status == "200") {
					location.reload();
					$('#success').css('display', 'block');
					$("#success").html("Contact Edited Succsessfully.");
					$('#success').delay(5000).fadeOut('slow');
				
				}
			},
			error: function(e) {
				$('#error').css('display', 'block');
				$("#error").html("Oops! something went wrong.");
				$('#error').delay(5000).fadeOut('slow');
				location.reload();
			}
		});

	});
});