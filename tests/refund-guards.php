<?php
// Focused connector tests: no network, no real orders/payment processing.
define('ABSPATH',__DIR__);
function register_deactivation_hook(...$x){}function register_activation_hook(...$x){}function add_action(...$x){}function add_filter(...$x){}
class WP_Error {function __construct(public $code,public $message,public $data=[]){ }function get_error_message(){return $this->message;}}
function is_wp_error($v){return $v instanceof WP_Error;}function sanitize_key($s){return $s;}function sanitize_textarea_field($s){return $s;}function add_option($k,$v,...$x){global $options;if(isset($options[$k]))return false;$options[$k]=$v;return true;}function delete_option($k){global $options;unset($options[$k]);}
function get_current_user_id(){return 1;}function wp_get_current_user(){return (object)['user_login'=>'staff'];}
class WC_Order {public $history=[];public $remaining=50;public $payment='cod';public $status='processing';function get_id(){return 12;}function get_meta($k){return $this->history;}function get_remaining_refund_amount(){return $this->remaining;}function get_payment_method(){return $this->payment;}function get_date_paid(){return false;}function get_status(){return $this->status;}function update_status(...$x){throw new Exception('Should not mutate');}}
class Req implements ArrayAccess{function __construct(public $data){}function get_param($k){return $this->data[$k]??null;}function offsetGet($k):mixed{return $this->data[$k]??null;}function offsetExists($k):bool{return isset($this->data[$k]);}function offsetSet($k,$v):void{$this->data[$k]=$v;}function offsetUnset($k):void{unset($this->data[$k]);}}
function wc_get_order($id){global $order;return $order;}
require __DIR__.'/../wordpress/loon-hearth-staff/loon-hearth-staff.php';
$options=[];$order=new WC_Order();$base=['id'=>12,'request_id'=>'12345678-1234-1234-1234-123456789012','amount'=>'10','reason'=>'Test','mode'=>'manual','money_returned'=>true];
function check($data,$phrase){global $options;$r=LH_Staff::refund(new Req($data));if(!($r instanceof WP_Error)||!str_contains($r->message,$phrase))throw new Exception('Guard failure: '.$phrase);if(isset($options['lhs_lock_12']))throw new Exception('Lock leaked');echo "PASS $phrase\n";}
check(array_replace($base,['amount'=>'51']),'exceeds');check(array_replace($base,['amount'=>'-1']),'positive');check(array_replace($base,['amount'=>'10.123']),'decimal');check(array_replace($base,['reason'=>'']),'reason');check(array_replace($base,['money_returned'=>false]),'confirmation');check(array_replace($base,['mode'=>'unexpected']),'Select');
$order->history=[$base['request_id']=>['state'=>'completed']];check($base,'already submitted');$order->history=['other'=>['state'=>'needs_review']];check($base,'previous refund');$order->history=['other'=>['state'=>'submitted']];check($base,'previous refund');$order->history=[];$order->payment='helcimjs';check($base,'cash/e-transfer');
$r=LH_Staff::status(new Req(['id'=>12,'status'=>'refunded','expected_status'=>'processing']));if(!$r instanceof WP_Error)throw new Exception('Refund status bypass');echo "PASS direct refunded status blocked\n";
$r=LH_Staff::status(new Req(['id'=>12,'status'=>'completed','expected_status'=>'on-hold']));if(!$r instanceof WP_Error||!str_contains($r->message,'changed'))throw new Exception('Stale status accepted');echo "PASS stale status blocked\n";
