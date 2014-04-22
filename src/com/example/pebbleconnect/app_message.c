#include <pebble.h>

Window *window;	
TextLayer *text_layer;
char greeting_buffer[32];

void process_tuple(Tuple *t);
void accel_tap_handler(AccelAxisType axis, int32_t direction);
void handle_init(void);
void handle_deinit(void);
// Key values for AppMessage Dictionary
enum {
	STATUS_KEY = 0,	
	MESSAGE_KEY = 1
};

void send_int(uint8_t key, uint8_t cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
     
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
     
    app_message_outbox_send();
}

// Called when a message is received from PebbleKitJS
static void in_received_handler(DictionaryIterator *received, void *context) {
	Tuple *tuple;
	//Get data
  Tuple *t = dict_read_first(received);
  if(t)
  {
    process_tuple(t);
  }
   
  //Get next
  while(t != NULL)
  {
    t = dict_read_next(received);
    if(t)
    {
      process_tuple(t);
    }
  }
}
void process_tuple(Tuple *t)
{
  //Get key
  int key = t->key;
 
  //Get string value
  char string_value[32];
  strcpy(string_value, t->value->cstring);
 
  //Decide what to do
  switch(key) {
    case STATUS_KEY:
      break;
    case MESSAGE_KEY:
      snprintf(greeting_buffer, sizeof("Greetings can be very long messages."), "%s", string_value);
      text_layer_set_text(text_layer, (char*) &greeting_buffer);
      break;
  }
}
void window_load(Window *window)
{
  text_layer = text_layer_create(GRect(0, 0, 144, 168));
  text_layer_set_background_color(text_layer, GColorClear);
  text_layer_set_text_color(text_layer, GColorBlack);
  text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
   
  layer_add_child(window_get_root_layer(window), (Layer*) text_layer);
  text_layer_set_text(text_layer, "Learn how to say hello, in any language!");
}
 
void window_unload(Window *window)
{
  text_layer_destroy(text_layer);
}

void init(void) {
	window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
	
	// Register AppMessage handlers
	app_message_register_inbox_received(in_received_handler); 
	app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
	
  handle_init();
  
  window_stack_push(window, true);
}

void deinit(void) {
	app_message_deregister_callbacks();
  handle_deinit();
	window_destroy(window);
}


void accel_tap_handler(AccelAxisType axis, int32_t direction) {
   send_int(7, 77);
}

void handle_init(void) {
  accel_tap_service_subscribe(&accel_tap_handler);
}

void handle_deinit(void) {
  accel_tap_service_unsubscribe();
}


int main( void ) {
	init();
	app_event_loop();
	deinit();
}