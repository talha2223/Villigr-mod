"""
Game Connection Handler.
Manages WebSocket connection between Python app and Minecraft mod.
"""

import asyncio
import json
import websockets
import threading
import time

class GameConnection:
    def __init__(self, host="localhost", port=9876):
        self.host = host
        self.port = port
        self.websocket = None
        self.connected = False
        self.on_message_callback = None
        self.on_connect_callback = None
        self.on_disconnect_callback = None
        self._server = None
        self._thread = None
    
    async def start_server(self):
        """Start WebSocket server for Minecraft mod to connect."""
        self._server = await websockets.serve(
            self._handle_client,
            self.host,
            self.port
        )
        print(f"[GameConnection] Server started on ws://{self.host}:{self.port}")
        await self._server.wait_closed()
    
    async def _handle_client(self, websocket, path):
        """Handle incoming connection from Minecraft mod."""
        self.websocket = websocket
        self.connected = True
        print(f"[GameConnection] Minecraft mod connected!")
        
        if self.on_connect_callback:
            self.on_connect_callback()
        
        try:
            async for message in websocket:
                data = json.loads(message)
                print(f"[GameConnection] Received: {data}")
                
                if self.on_message_callback:
                    self.on_message_callback(data)
                    
        except websockets.exceptions.ConnectionClosed:
            print("[GameConnection] Mod disconnected")
        finally:
            self.connected = False
            self.websocket = None
            
            if self.on_disconnect_callback:
                self.on_disconnect_callback()
    
    def start_background(self):
        """Start the server in a background thread."""
        def run():
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            loop.run_until_complete(self.start_server())
        
        self._thread = threading.Thread(target=run, daemon=True)
        self._thread.start()
    
    async def send_command(self, command):
        """Send a command to the Minecraft mod."""
        if self.websocket and self.connected:
            message = json.dumps(command)
            await self.websocket.send(message)
            print(f"[GameConnection] Sent: {command}")
            return True
        else:
            print("[GameConnection] Not connected to mod!")
            return False
    
    def send_action(self, action):
        """Send an action command (blocking wrapper)."""
        if not self.connected:
            print("[GameConnection] Cannot send action - not connected")
            return False
        
        loop = asyncio.new_event_loop()
        try:
            result = loop.run_until_complete(self.send_command(action))
            return result
        finally:
            loop.close()
    
    def send_chat(self, text):
        """Send a chat message to the game."""
        return self.send_action({
            "type": "CHAT",
            "message": text
        })
    
    def send_villager_action(self, action_type, **kwargs):
        """Send a villager action."""
        command = {
            "type": "VILLAGER_ACTION",
            "action": action_type,
            **kwargs
        }
        return self.send_action(command)
