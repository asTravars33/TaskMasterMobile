import numpy as np
import tensorflow as tf
import os


def ids_to_text(ids):
  return tf.strings.reduce_join(id_to_char(ids), axis=-1)

class TextModel(tf.keras.Model):
  def __init__(self, vocab_size, embedding_dim, rnn_units):
    super().__init__(self)
    # The different layers
    self.embedding = tf.keras.layers.Embedding(vocab_size, embedding_dim)
    self.gru = tf.keras.layers.GRU(rnn_units, return_sequences=True, return_state=True) # LSTM???
    '''self.lstm = tf.keras.layers.LSTM(
            input_size=128,
            hidden_size=128,
            num_layers=3,
            dropout=0.2,
        )'''
    self.dense = tf.keras.layers.Dense(vocab_size)
  def call(self, inputs, states=None, return_state=False, training=False): # Propagate through the model
    x = inputs
    x = self.embedding(x, training=training)
    if states is None:
      states = self.gru.get_initial_state(x)
    x, states = self.gru(x, initial_state=states, training=training)
    x = self.dense(x, training=training)

    if return_state:
      return x, states
    return x

class Predictor(tf.keras.Model):
  def __init__(self, model, id_to_char, char_to_id, temperature=1.0):
    super().__init__()
    self.temperature = temperature
    self.model = model
    self.id_to_char = id_to_char
    self.char_to_id = char_to_id

  def generate_one_step(self, inputs, states=None):
    # Convert strings to token IDs.
    input_chars = tf.strings.unicode_split(inputs, 'UTF-8')
    input_ids = self.char_to_id(input_chars).to_tensor()

    # Run the model.
    # predicted_logits.shape is [batch, char, next_char_logits]
    predicted_logits, states = self.model(inputs=input_ids, states=states,
                                          return_state=True)
    # Only use the last prediction.
    predicted_logits = predicted_logits[:, -1, :]
    predicted_logits = predicted_logits/self.temperature

    # Sample the output logits to generate token IDs.
    predicted_ids = tf.random.categorical(predicted_logits, num_samples=1)
    predicted_ids = tf.squeeze(predicted_ids, axis=-1)

    # Convert from token ids to characters
    predicted_chars = self.id_to_char(predicted_ids)

    # Return the characters and model state.
    return predicted_chars, states

def predict(prompt):
    vocab = [' ', '!', '"', "'", ',', '-', '.', '0', '1', '2', '3', '5', ':', '?', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z']
    char_to_id = tf.keras.layers.StringLookup(vocabulary=list(vocab), mask_token=None)
    id_to_char = tf.keras.layers.StringLookup(vocabulary=char_to_id.get_vocabulary(), invert=True, mask_token=None)

    # Dataset
    seq_length=100
    BATCH_SIZE=64
    BUFFER_SIZE=10000 # How much to shuffle at once

    # Model
    embedding_dim=256
    rnn_units=1024

    vocab_size = len(char_to_id.get_vocabulary())
    model = TextModel(vocab_size = vocab_size, embedding_dim=embedding_dim, rnn_units=rnn_units)

    model.load_weights(checkpoint_path)

    predictor = Predictor(model, id_to_char, char_to_id)

    next_char = tf.constant([prompt])
    result = [next_char]
    states = None

    for n in range(400):
      next_char, states = predictor.generate_one_step(next_char, states=states)
      result.append(next_char)

    result = tf.strings.join(result)
    return result