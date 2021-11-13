export class AttachmentFile {
    public fileName:string;
    public fileContent:string; // base64 content
    public mimeType:string;

    public constructor(fileName_:string, fileContent_:string, mimeType_:string){
        this.fileName = fileName_;
        this.fileContent = fileContent_;
        this.mimeType = mimeType_;
    }

}