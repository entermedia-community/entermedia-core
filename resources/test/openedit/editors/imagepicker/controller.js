/**
  All of this javascript could be imbedded in ImagePickerController class
  for ease of reuse.  It is probably more efficient to have it here.
*/

function Controller()
{
	this.location = null;
	this.homeForm = null;
	
	this.gotoFileUpload = Controller_gotoFileUpload;
	this.refreshTree = Controller_refreshTree;
	this.uploadFile = Controller_uploadFile;
}

function Controller_gotoFileUpload()
{
	this.homeForm.action = this.location +'/index.html?wsp-action=tree.setPage&WebTreeName=imagePicker&path=' + this.location + '/uploadfile.html';
	this.homeForm.target = "_self";
	this.homeForm.submit();
}

function Controller_refreshTree()
{
	this.homeForm.action = this.location +'/index.html?controller-command=reloadTree';
	this.homeForm.target = "_self";
	this.homeForm.submit();
}

function Controller_uploadFile( inForm )
{
	if ( inForm == null )
	{
		inForm = this.homeForm;
	}
	inForm.action = this.location +'/index.html?controller-command=uploadFile';
	inForm.target = "_self";
	inForm.submit();
}
