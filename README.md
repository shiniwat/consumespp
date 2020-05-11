# ConsumeSPPServices

ConsumeSPPServices basically consumes all available SPP resources, so that Android system runs into the situation where no more SPP resources can be created.

## How to run
Just start and run the apk.

The app shows the TextView that tells how many SPP services are created with this app.

## How to verify the SPP service records
In order to verify how many SPP service records are created on the device, you should use sdptool (available in most Linux distribution).

For instance, this is basic steps to verify:

1. On Linux box, establish Bluetooth pairing with the target Android device.
2. Check to see Android BT MAC address, which will be given to sdptool command
3. On Android device, install and run ConsumeSPPServices app.
4. On the linux box, browse the SPP service records by following command:

```
sdptool browse <BT address of the Android device>
```

For instance,
```
$ sdptool browse ac:37:43:88:b8:d5
```

You'll see something like this:

```
Browsing AC:37:43:88:B8:D5 ...
Service Name: ConsumingSPPServices
Service RecHandle: 0x1000e
Service Class ID List:
  UUID 128: 886da01f-9abd-4d9d-80c7-02af85c822a8
Protocol Descriptor List:
  "L2CAP" (0x0100)
  "RFCOMM" (0x0003)
    Channel: 8
    

Service Name: ConsumingSPPServices
Service RecHandle: 0x1000f
Service Class ID List:
  UUID 128: 886da01f-9abd-4d9d-80c7-02af85c822a8
Protocol Descriptor List:
  "L2CAP" (0x0100)
  "RFCOMM" (0x0003)
    Channel: 9

...    
```
