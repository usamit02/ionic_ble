import { Component ,OnInit} from '@angular/core';
import { Plugins } from '@capacitor/core';
import { ToastController } from '@ionic/angular';
const { Toast } = Plugins;
const { CustomNativePlugin } = Plugins;
@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage implements OnInit{
  devices:Array<{}>=[];
  constructor(public toastController: ToastController) {}
  ngOnInit(){
    CustomNativePlugin.customCall({ message: "CUSTOM MESSAGE" });
    CustomNativePlugin.customFunction();
    Plugins.CustomNativePlugin.addListener("scan",device=>{
      Object.keys(device).forEach(key=>{
        this.devices.push({mac:key,na:device[key]});
      });
      this.devices=this.devices.concat([]);      
    });    
  }
  init(){
    CustomNativePlugin.init().then(res=>{
       console.log(`init success:${res}`);
    }).catch(err=>{
       console.log(`init error:${err}`);
    });
  }
  scan(){
    CustomNativePlugin.scan().then(res=>{
      console.log(`scan success:${res}`);
    }).catch(err=>{
      console.log(`scan error:${err}`);
    });
  }
  gatt(mac){
    CustomNativePlugin.gatt({mac:mac}).then(res=>{
      console.log(`gatt success:${res}`);
    }).catch(err=>{
      console.log(`gatt error:${err}`);
    });
  } 
  write(val){
    CustomNativePlugin.write({val:val}).then(res=>{
      console.log(`write success:${res}`);
    }).catch(err=>{
      console.log(`write error:${err}`);
    });
  }  
  async show() {
    await Toast.show({
      text: 'Hello!'
    });
  }
  async toast() {
    const toast = await this.toastController.create({
      message: 'Your settings have been saved.',
      duration: 2000
    });
    toast.present();
  }
}
