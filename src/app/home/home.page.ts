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

  constructor(public toastController: ToastController) {}
  ngOnInit(){
    CustomNativePlugin.customCall({ message: "CUSTOM MESSAGE" });
    CustomNativePlugin.customFunction();
    CustomNativePlugin.bleInit();
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
